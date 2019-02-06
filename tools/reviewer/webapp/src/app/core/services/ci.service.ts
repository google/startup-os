import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BranchInfo, CiResponse, Commit, Diff } from '@/core/proto';
import { LocalserverService } from './localserver.service';

export interface Status {
  projectId: string;
  message: string;
  color: string;
}

export interface CiLog {
  log: string;
  status: Status;
}

@Injectable()
export class CiService {
  constructor(private localserverService: LocalserverService) { }

  // Gets statuses for each repo on diff page
  loadStatusList(diff: Diff): Observable<Status[]> {
    return new Observable(observer => {
      // Get branchInfoList from localserver
      this.localserverService
        .getBranchInfoList(diff.getId(), diff.getWorkspace())
        .subscribe(branchInfoList => {
          // Find all statuses that match repo id and last commit id
          const statusList: Status[] = [];
          for (const branchInfo of branchInfoList) {
            const lastCiResponse: CiResponse = diff.getCiResponseList()[0];
            for (const targetResult of lastCiResponse.getResultList()) {
              const status: Status = this.getStatus(branchInfo, targetResult);
              if (status) {
                statusList.push(status);
              }
            }
          }
          observer.next(statusList);
        });
    });
  }

  // Gets status and log for specific repo on log page
  loadCiLog(diff: Diff, projectId: string): Observable<CiLog> {
    return new Observable(observer => {
      // Get branchInfoList from localserver
      this.localserverService
        .getBranchInfoList(diff.getId(), diff.getWorkspace())
        .subscribe(branchInfoList => {
          for (const branchInfo of branchInfoList) {
            // Find status that match repo id and last commit id
            const lastCiResponse: CiResponse = diff.getCiResponseList()[0];
            for (const targetResult of lastCiResponse.getResultList()) {
              if (targetResult.getTarget().getRepo().getId() === projectId) {
                const status: Status = this.getStatus(branchInfo, targetResult);
                if (status) {
                  const ciLog: CiLog = {
                    status: status,
                    log: targetResult.getLog(),
                  };
                  observer.next(ciLog);
                  break;
                }
              }
            }
          }
        });
    });
  }

  private getStatus(
    branchInfo: BranchInfo,
    targetResult: CiResponse.TargetResult,
  ): Status {
    if (branchInfo.getRepoId() === targetResult.getTarget().getRepo().getId()) {
      const commits: Commit[] = branchInfo.getCommitList();
      // Take last commit (newest change)
      const commitId: string = commits.reverse()[0].getId();

      // Set status from the result,
      // or set outdated status if result with the commit not found.
      const status: Status = (commitId === targetResult.getTarget().getCommitId()) ?
        this.convertTargetResult(targetResult.getStatus()) :
        this.convertTargetResult(CiResponse.TargetResult.Status.OUTDATED);

      status.projectId = targetResult.getTarget().getRepo().getId();
      return status;
    }
  }

  // Converts enum to status
  private convertTargetResult(status: CiResponse.TargetResult.Status): Status {
    switch (status) {
      case CiResponse.TargetResult.Status.SUCCESS:
        return { message: 'Passed', color: '#12a736', projectId: '' };
      case CiResponse.TargetResult.Status.FAIL:
        return { message: 'Failed', color: '#db4040', projectId: '' };
      case CiResponse.TargetResult.Status.RUNNING:
        return { message: 'Running', color: '#1545bd', projectId: '' };
      case CiResponse.TargetResult.Status.OUTDATED:
        return { message: 'Outdated', color: '#808080', projectId: '' };
    }
  }
}
