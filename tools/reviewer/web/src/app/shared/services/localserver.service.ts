import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs';

import {
  BranchInfo,
  DiffFilesRequest,
  DiffFilesResponse,
  File,
  TextDiffRequest,
  TextDiffResponse,
} from '@/shared/shell';
import { EncodingService } from './encoding.service';
import { NotificationService } from './notification.service';

@Injectable()
export class LocalserverService {
  constructor(
    private http: Http,
    private encodingService: EncodingService,
    private notificationService: NotificationService,
  ) { }

  getBranchInfo(id: number, workspace: string): Observable<BranchInfo> {
    return new Observable(observer => {
      // Create diff files request
      const diffFilesRequest = new DiffFilesRequest();
      diffFilesRequest.setDiffId(id);
      diffFilesRequest.setWorkspace(workspace);
      const requestBinary = diffFilesRequest.serializeBinary();
      const requestBase64 = this.encodingService
        .encodeUint8ArrayToBase64String(requestBinary);

      // Send the request to local server
      this.http
        .get('http://localhost:7000/get_diff_files?request=' + requestBase64)
        .map(response => response.text())
        .subscribe(diffFilesResponseBase64 => {
          // Decode response
          const diffFilesResponseBinary = this.encodingService
            .decodeBase64StringToUint8Array(diffFilesResponseBase64);
          const diffFilesResponse: DiffFilesResponse = DiffFilesResponse
            .deserializeBinary(diffFilesResponseBinary);

          if (diffFilesResponse.getBranchinfoList().length === 0) {
            observer.error();
            this.notificationService.error('Local server: Branches not found');
          }

          observer.next(diffFilesResponse.getBranchinfoList()[0]);
        }, () => {
          this.error();
          observer.error();
        });
    });
  }

  getFileChanges(
    leftFile: File,
    rightFile: File,
  ): Observable<TextDiffResponse> {
    return new Observable(observer => {
      // Create text diff request
      const textDiffRequest = new TextDiffRequest();
      textDiffRequest.setLeftFile(leftFile);
      textDiffRequest.setRightFile(rightFile);
      const requestBinary = textDiffRequest.serializeBinary();
      const requestBase64 = this.encodingService
        .encodeUint8ArrayToBase64String(requestBinary);

      // Send the request to local server
      this.http
        .get('http://localhost:7000/get_text_diff?request=' + requestBase64)
        .map(response => response.text())
        .subscribe(textDiffResponseBase64 => {
          // Decode response
          const textDiffResponseBinary = this.encodingService
            .decodeBase64StringToUint8Array(textDiffResponseBase64);
          const textDiffResponse: TextDiffResponse = TextDiffResponse
            .deserializeBinary(textDiffResponseBinary);

          observer.next(textDiffResponse);
        }, () => {
          this.error();
          observer.error();
        });
    });
  }

  getDiffFiles(id: number, workspace: string): Observable<File[]> {
    return new Observable(observer => {
      this.getBranchInfo(id, workspace).subscribe(branchInfo => {
        let files: File[] = branchInfo.getUncommittedFileList();
        for (const commit of branchInfo.getCommitList()) {
          // Remove duplicates
          let newfiles: File[] = commit.getFileList();
          newfiles = newfiles.filter(newfile => {
            for (const file of files) {
              if (newfile.getFilename() === file.getFilename()) {
                return false;
              }
            }
            return true;
          });

          files = files.concat(newfiles);
        }

        observer.next(files);
      });
    });
  }

  error(): void {
    this.notificationService.error("Local server doesn't respond");
  }
}
