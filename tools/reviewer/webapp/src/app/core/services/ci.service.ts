import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';

import { BranchInfo, CiResponse, Commit, Diff } from '@/core/proto';
import { LocalserverService } from './localserver.service';

export interface Status {
  repoId: string;
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
    return new Observable((observer: Subscriber<Status[]>) => {
      // Get branchInfoList from localserver
      this.localserverService
        .getBranchInfoList(diff.getId(), diff.getWorkspace())
        .subscribe((branchInfoList: BranchInfo[]) => {
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
  loadCiLog(diff: Diff, repoId: string): Observable<CiLog> {
    return new Observable((observer: Subscriber<CiLog>) => {
      // Get branchInfoList from localserver
      this.localserverService
        .getBranchInfoList(diff.getId(), diff.getWorkspace())
        .subscribe((branchInfoList: BranchInfo[]) => {
          const ciLog: CiLog = this.getCiLog(diff, repoId, branchInfoList);
          if (ciLog) {
            observer.next(ciLog);
          } else {
            observer.error();
          }
        });
    });
  }

  private getCiLog(
    diff: Diff,
    repoId: string,
    branchInfoList: BranchInfo[],
  ): CiLog {
    for (const branchInfo of branchInfoList) {
      // Find status that match repo id and last commit id
      const lastCiResponse: CiResponse = diff.getCiResponseList()[0];
      for (const targetResult of lastCiResponse.getResultList()) {
        if (targetResult.getTarget().getRepo().getId() === repoId) {
          const status: Status = this.getStatus(branchInfo, targetResult);
          if (status) {
            return {
              status: status,
              log: targetResult.getLog(),
            };
          }
        }
      }
    }
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

      status.repoId = targetResult.getTarget().getRepo().getId();
      return status;
    }
  }

  // Converts enum to status
  private convertTargetResult(status: CiResponse.TargetResult.Status): Status {
    switch (status) {
      case CiResponse.TargetResult.Status.SUCCESS:
        return { message: 'Passed', color: '#12a736', repoId: '' };
      case CiResponse.TargetResult.Status.FAIL:
        return { message: 'Failed', color: '#db4040', repoId: '' };
      case CiResponse.TargetResult.Status.RUNNING:
        return { message: 'Running', color: '#1545bd', repoId: '' };
      case CiResponse.TargetResult.Status.OUTDATED:
        return { message: 'Outdated', color: '#808080', repoId: '' };
    }
  }
}
