// NOTICE: it's actually not a diff, it's a two files changes
// TODO: rename the component and linked files
// e.g. FileChangeComponent

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Http } from '@angular/http';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Comment,
  Diff,
  DiffFilesRequest,
  File,
  TextDiffRequest,
  TextDiffResponse,
  Thread,
} from '@/shared';
import {
  DifferenceService,
  EncodingService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { DiffService } from './diff.service';
import { MockServerService } from './mock-server.service';

// The component implements a diff
@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss'],
  providers: [MockServerService],
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  textDiffResponse: TextDiffResponse;
  changes: number[];
  localThreads: Thread[];
  diff: Diff;
  newCommentSubscription: Subscription;
  file = new File();
  branchInfo: BranchInfo;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService,
    private encodingService: EncodingService,
    private notificationService: NotificationService,
    private http: Http,
    private mockServerService: MockServerService,
  ) {
    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        this.addComment(param.lineNumber, param.comments);
      },
    );
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    const filename = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    this.file.setFilename(filename);

    const diffId = urlSnapshot.url[0].path;
    this.firebaseService.getDiff(diffId).subscribe(
      diff => {
        this.diff = diff;
        this.file.setWorkspace(this.diff.getWorkspace());
        this.localThreads = this.diff
          .getThreadList()
          .filter(v => v.getFile().getFilename() === this.file.getFilename());

        this.getBranchInfo();
      },
      () => {
        // Access denied
      },
    );
  }

  getBranchInfo(): void {
    const diffFilesRequest = new DiffFilesRequest();
    diffFilesRequest.setWorkspace(this.diff.getWorkspace());
    diffFilesRequest.setDiffId(this.diff.getId());

    const requestBinary = diffFilesRequest.serializeBinary();
    const requestBase64 = this.encodingService
      .encodeUint8ArrayToBase64String(requestBinary);
    const url = 'http://localhost:7000/get_text_diff?request=' + requestBase64;

    // TODO: use http instead
    this.mockServerService
      .getMockBranchInfo(url, this.diff)
      .subscribe(diffFilesResponse => {
        const firstBranchInfo = diffFilesResponse.getBranchinfoList()[0];
        this.branchInfo = firstBranchInfo;
        this.file.setRepoId(this.branchInfo.getRepoId());
        this.file.setCommitId(this.branchInfo.getCommitList()[0].getId());

        this.getFileChanges();
      });
  }

  getFileChanges(): void {
    const textDiffRequest = new TextDiffRequest();

    // Temporarily hardcoded data
    // TODO: Take data from this.branchInfo and this.file instead
    const leftFile = new File();
    leftFile.setFilename('tools/reviewer/service/TestTool.java');
    leftFile.setRepoId(this.branchInfo.getRepoId());
    leftFile.setCommitId('9cd8786e852f8a2992d3b53f5d2daa4399622051');
    const rightFile = new File();
    rightFile.setFilename('tools/reviewer/service/TestTool.java');
    rightFile.setRepoId(this.branchInfo.getRepoId());
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

  // Send diff with new comment to firebase
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
    newThread.setRepoId(this.branchInfo.getRepoId());
    newThread.setCommitId(this.file.getCommitId());
    newThread.setFile(this.file);

    return newThread;
  }

  ngOnDestroy() {
    this.newCommentSubscription.unsubscribe();
  }
}
