// NOTICE: it's actually not a diff, it's a two files changes
// TODO: rename the component and linked files
// e.g. FileChangeComponent

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import {
  Comment,
  Diff,
  File,
  TextDiffRequest,
  TextDiffResponse,
  Thread
} from '@/shared';
import {
  DifferenceService,
  EncodingService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { DiffService } from './diff.service';

// The component implements a diff
@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss']
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  textDiffResponse: TextDiffResponse;
  changes: number[];
  localThreads: Thread[];
  diff: Diff;
  newCommentSubscription: Subscription;
  filename: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService,
    private encodingService: EncodingService,
    private notificationService: NotificationService,
    private http: Http,
  ) {
    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        this.addComment(param.lineNumber, param.comments);
      }
    );
  }

  getFileChanges(): void {
    const textDiffRequest = new TextDiffRequest();

    // Temporarily hardcoded data
    // TODO: take the data from somewhere else (url?)
    const leftFile = new File();
    leftFile.setFilename('tools/reviewer/service/TestTool.java');
    leftFile.setRepoId('startup-os');
    leftFile.setCommitId('9cd8786e852f8a2992d3b53f5d2daa4399622051');
    const rightFile = new File();
    rightFile.setFilename('tools/reviewer/service/TestTool.java');
    rightFile.setRepoId('startup-os');
    rightFile.setCommitId('6dadc371ec040664c1491813e7548bfd0cca4434');

    textDiffRequest.setLeftFile(leftFile);
    textDiffRequest.setRightFile(rightFile);

    const requestBinary = textDiffRequest.serializeBinary();
    const requestBase64 = this.encodingService
      .encodeUint8ArrayToBase64String(requestBinary);

    this.http
      .get('http://localhost:7000/get_text_diff?request=' + requestBase64)
      .map(response => response.text())
      .subscribe(textDiffResponse => {
        const responsBinary = this.encodingService
          .decodeBase64StringToUint8Array(textDiffResponse);
        this.textDiffResponse = TextDiffResponse
          .deserializeBinary(responsBinary);

        // TODO: use textDiffResponse.getChangesList() instead
        this.changes = this.differenceService.compare(
          this.textDiffResponse.getLeftFileContents(),
          this.textDiffResponse.getRightFileContents(),
        );

        this.isLoading = false;
      }, () => {
        this.notificationService.error('Local server error');
      });
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    this.filename = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');

    const diffId = urlSnapshot.url[0].path;
    this.firebaseService.getDiff(diffId).subscribe(
      diff => {
        this.diff = diff;
        this.localThreads = this.diff
          .getThreadList()
          .filter(v => v.getFile().getFilename() === this.filename);

        this.getFileChanges();
      },
      () => {
        // Access denied
      }
    );
  }

  addComment(lineNumber: number, comments: Comment[]): void {
    if (comments.length === 1) {
      // Create new thread
      const newThread: Thread = this.createNewThread(lineNumber, comments);
      this.diff.addThread(newThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe();
  }

  createNewThread(lineNumber: number, comments: Comment[]): Thread {
    const newThread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);

    // Temperoally hardcoded data
    // TODO: take the data from localserver response
    newThread.setRepoId('startup-os');
    newThread.setCommitId('hardcoded-id');
    const file = new File();
    file.setFilename(this.filename);
    file.setRepoId(newThread.getRepoId());
    file.setWorkspace(this.diff.getWorkspace());
    newThread.setFile(file);

    return newThread;
  }

  ngOnDestroy() {
    this.newCommentSubscription.unsubscribe();
  }
}
