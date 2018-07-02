// Hardcoded data
// TODO: use localserver response instead
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  BranchInfo,
  Commit,
  Diff,
  DiffFilesResponse,
  File,
} from '@/shared';

@Injectable()
export class MockServerService {
  getMockFiles(workspace: string): File[] {
    const filenames = [
      'aa/aa_commands.py',
      'review_server/local_firebase.py',
      'aa/aa_tool.py',
    ];
    const fileList: File[] = [];
    let index = 0;
    for (const filename of filenames) {
      const file = new File();
      file.setFilename(filename);
      file.setRepoId('startup-os');
      file.setWorkspace(workspace);
      file.setCommitId('hardcoded-commit-id-' + index++);
      fileList.push(file);
    }

    return fileList;
  }

  getMockBranchInfo(url: string, diff: Diff): Observable<DiffFilesResponse> {
    return new Observable(observer => {
      const branchInfo = new BranchInfo();
      branchInfo.setDiffId(diff.getId());
      branchInfo.setRepoId('startup-os');

      for (let i = 0; i < 2; i++) {
        const commit = new Commit();
        commit.setId('hardcoded-commit-id-' + i);
        commit.setFileList(this.getMockFiles(diff.getWorkspace()));
        branchInfo.addCommit(commit);
      }

      const diffFilesResponse = new DiffFilesResponse();
      diffFilesResponse.addBranchinfo(branchInfo);
      observer.next(diffFilesResponse);
    });
  }
}
