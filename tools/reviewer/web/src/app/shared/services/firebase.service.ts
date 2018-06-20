// NOTICE: It's temporarily code to support json from firebase

import { Injectable } from '@angular/core';
import { AngularFireDatabase, AngularFireObject } from 'angularfire2/database';
import { Observable } from 'rxjs/Observable';

import {
  Comment,
  Diff,
  File,
  Snapshot,
  Thread
} from '@/shared/shell/proto/code-review_pb';
// TODO: work with proto binary instead of json
import { FirebaseJsonService } from './firebase-json.service';
import { JsonDiff, JsonSnapshot, JsonThread } from './json-diff.interface';

@Injectable()
export class FirebaseService {
  constructor(private firebaseJsonService: FirebaseJsonService) { }

  getDiffs(): Observable<Diff[]> {
    return new Observable(observer => {
      this.firebaseJsonService.getDiffs().subscribe(diffs => {
        const protoDiffs: Diff[] = [];
        for (const diffId in diffs) {
          const diff = diffs[diffId];
          const protoDiff = this.fromJson(diff);
          // TODO: use string id instead of int number
          // Probably it'll be relevant when using firestore.
          protoDiff.setNumber(parseInt(diffId, 10));
          protoDiffs.push(protoDiff);
        }
        observer.next(protoDiffs);
      });
    });
  }

  getDiff(id: string): Observable<Diff> {
    return new Observable(observer => {
      this.firebaseJsonService.getDiff(id).subscribe(diff => {
        const protoDiff = this.fromJson(diff);
        observer.next(protoDiff);
      });
    });
  }

  updateDiff(diff: Diff): Observable<void> {
    const jsonDiff = this.toJson(diff);
    return this.firebaseJsonService.updateDiff(jsonDiff);
  }

  // Diff.AsObject != JsonDiff
  // Firebase includes JsonDiff at the moment
  // Proto functions includes Diff (and Diff.AsObject)
  fromJson(jsonDiff: JsonDiff): Diff {
    const diff = new Diff();
    diff.setNumber(jsonDiff.number);
    diff.setAuthor(jsonDiff.author);
    diff.setDescription(jsonDiff.description);
    diff.setBug(jsonDiff.bug);
    diff.setStatus(jsonDiff.status);
    diff.setWorkspace(jsonDiff.workspace);
    diff.setCreatedTimestamp(jsonDiff.createdTimestamp);
    diff.setModifiedTimestamp(jsonDiff.modifiedTimestamp);
    diff.setPullRequestId(jsonDiff.pullRequestId);

    diff.setReviewersList(jsonDiff.reviewers);
    diff.setNeedAttentionOfList(jsonDiff.needAttentionOf);
    diff.setCcList(jsonDiff.cc);

    if (jsonDiff.threads) {
      for (const jsonThread of jsonDiff.threads) {
        const thread = new Thread();
        thread.setSnapshot(jsonThread.snapshot);
        thread.setFilename(jsonThread.filename);
        thread.setLineNumber(jsonThread.lineNumber);
        thread.setIsDone(jsonThread.isDone);

        for (const jsonComment of jsonThread.comments) {
          const comment = new Comment();
          comment.setContent(jsonComment.content);
          comment.setCreatedBy(jsonComment.createdBy);
          comment.setTimestamp(jsonComment.timestamp);
          thread.addComments(comment);
        }
        diff.addThreads(thread);
      }
    }

    if (jsonDiff.snapshots) {
      for (const jsonSnapshot of jsonDiff.snapshots) {
        const snapshot = new Snapshot();
        snapshot.setForReview(jsonSnapshot.forReview);
        snapshot.setGitCommit(jsonSnapshot.gitCommit);
        snapshot.setTimestamp(jsonSnapshot.timestamp);

        snapshot.setFilesList(jsonSnapshot.files);
        diff.addSnapshots(snapshot);
      }
    }

    if (jsonDiff.files) {
      for (const jsonFile of jsonDiff.files) {
        const file = new File();
        file.setFileAction(jsonFile.fileAction);
        file.setFilePosition(jsonFile.filePosition);
        diff.addFiles(file);
      }
    }

    return diff;
  }

  toJson(diff: Diff): JsonDiff {
    const diffAsObject: Diff.AsObject = diff.toObject();

    const jsonThreads: JsonThread[] = [];
    for (const thread of diffAsObject.threadsList) {
      const jsonThread: JsonThread = {
        snapshot: thread.snapshot,
        filename: thread.filename,
        lineNumber: thread.lineNumber,
        isDone: thread.isDone,
        comments: thread.commentsList
      };
      jsonThreads.push(jsonThread);
    }

    const jsonSnapshots: JsonSnapshot[] = [];
    for (const snapshot of diffAsObject.snapshotsList) {
      const jsonSnapshot: JsonSnapshot = {
        timestamp: snapshot.timestamp,
        gitCommit: snapshot.gitCommit,
        forReview: snapshot.forReview,
        files: snapshot.filesList
      };
      jsonSnapshots.push(jsonSnapshot);
    }

    const jsonDiff: JsonDiff = {
      number: diff.getNumber(),
      author: diff.getAuthor(),
      reviewers: diffAsObject.reviewersList,
      description: diffAsObject.description,
      bug: diff.getBug(),
      status: diff.getStatus(),
      workspace: diff.getWorkspace(),
      createdTimestamp: diff.getCreatedTimestamp(),
      modifiedTimestamp: diff.getModifiedTimestamp(),
      files: diffAsObject.filesList,
      snapshots: jsonSnapshots,
      threads: jsonThreads,
      pullRequestId: diff.getPullRequestId(),
      needAttentionOf: diffAsObject.needAttentionOfList,
      cc: diffAsObject.ccList
    };

    return jsonDiff;
  }
}
