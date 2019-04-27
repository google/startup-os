import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BranchInfo, Diff, File, TextDiff, Thread } from '@/core/proto';
import { LocalserverService } from './localserver.service';

export interface TextDiffReturn {
  branchInfo: BranchInfo;
  leftFile: File;
  rightFile: File;
  filesSortedByCommits: File[];
  textDiff: TextDiff;
  localThreads: Thread[];
}

// The service loads all required data to launch code-changes
@Injectable()
export class TextDiffService {
  constructor(private localserverService: LocalserverService) { }

  load(
    diff: Diff,
    filenameWithRepo: string,
    defaultLeftCommitId?: string,
    defaultRightCommitId?: string,
  ): Observable<TextDiffReturn> {
    return new Observable(observer => {
      // Get branchInfoList from localserver
      this.localserverService
        .getBranchInfoList(
          diff.getId(),
          diff.getWorkspace(),
      )
        .subscribe(branchInfoList => {
          // Get file and branchInfo from branchInfoList
          try {
            const { branchInfo, file } = this.localserverService.getFileData(
              filenameWithRepo,
              branchInfoList,
            );

            // Create commits
            const {
              leftFile,
              rightFile,
              filesSortedByCommits,
            } = this.getFiles(
              file,
              branchInfo,
              defaultLeftCommitId,
              defaultRightCommitId,
              );

            // Create local threads
            const localThread: Thread[] = this.getLocalThreads(
              diff,
              file,
              leftFile.getCommitId(),
              rightFile.getCommitId(),
            );

            // Load textDiff
            this.localserverService
              .getFileChanges(leftFile, rightFile)
              .subscribe(textDiffResponse => {
                const textDiff: TextDiff = textDiffResponse.getTextDiff();

                observer.next({
                  branchInfo: branchInfo,
                  leftFile: leftFile,
                  rightFile: rightFile,
                  filesSortedByCommits: filesSortedByCommits,
                  textDiff: textDiff,
                  localThreads: localThread,
                });
              });
          } catch (e) {
            observer.error(e);
          }
        });
    });
  }

  // Returns sorted file list and left & right selected files
  private getFiles(
    file: File,
    branchInfo: BranchInfo,
    leftCommitId: string,
    rightCommitId: string,
  ): {
      leftFile: File;
      rightFile: File;
      filesSortedByCommits: File[];
    } {
    const filesSortedByCommits: File[] = this.localserverService.getFilesSortedByCommits(
      file,
      branchInfo,
    );
    const firstIndex: number = 0;
    const lastIndex: number = filesSortedByCommits.length - 1;
    const headCommitId: string = filesSortedByCommits[firstIndex].getCommitId();
    const lastCommitId: string = filesSortedByCommits[lastIndex].getCommitId();
    leftCommitId = leftCommitId || headCommitId;
    rightCommitId = rightCommitId || lastCommitId;

    let leftIndex: number = this.addCommitId(leftCommitId, filesSortedByCommits);
    let rightIndex: number = this.addCommitId(rightCommitId, filesSortedByCommits);

    if (leftIndex === -1 || rightIndex === -1) {
      throw new Error('Commit not found');
    }

    // Switch left and right commits, if left commit id is newer than right one.
    // This cannot happen using the UI, but can by editing the url.
    if (leftIndex >= rightIndex) {
      [leftIndex, rightIndex] = [rightIndex, leftIndex];
    }

    return {
      leftFile: this.getFile(leftIndex, filesSortedByCommits),
      rightFile: this.getFile(rightIndex, filesSortedByCommits),
      filesSortedByCommits: filesSortedByCommits,
    };
  }

  // Add commit to commit list, if the commit isn't present already in the list.
  private addCommitId(commitId: string, filesSortedByCommits: File[]): number {
    const fileIndex: number = this.getFileIndex(commitId, filesSortedByCommits);
    if (fileIndex === -1) {
      const uncommitedFile = new File();
      uncommitedFile.setCommitId(commitId);
      filesSortedByCommits.push(uncommitedFile);
    }
    return fileIndex;
  }

  private getFile(fileIndex: number, filesSortedByCommits: File[]): File {
    return (fileIndex !== -1) ? filesSortedByCommits[fileIndex] : new File();
  }

  private getFileIndex(commitId: string, filesSortedByCommits: File[]): number {
    let fileIndex: number = -1;
    filesSortedByCommits.some((file, index) => {
      if (file.getCommitId() === commitId) {
        fileIndex = index;
        return true; // break
      }
    });
    return fileIndex;
  }

  private getLocalThreads(
    diff: Diff,
    file: File,
    leftCommitId: string,
    rightCommitId: string,
  ): Thread[] {
    return diff.getCodeThreadList()
      .filter(thread => {
        const filenamesAreEqual: boolean =
          thread.getFile().getFilenameWithRepo() ===
          file.getFilenameWithRepo();

        const isCurrentCommitId: boolean =
          thread.getCommitId() === leftCommitId ||
          thread.getCommitId() === rightCommitId;

        return filenamesAreEqual && isCurrentCommitId;
      });
  }
}
