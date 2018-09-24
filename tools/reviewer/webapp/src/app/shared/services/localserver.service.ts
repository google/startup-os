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
} from '@/shared/proto';
import { EncodingService } from './encoding.service';
import { NotificationService } from './notification.service';

export interface FileData {
  branchInfo: BranchInfo;
  file: File;
}

@Injectable()
export class LocalserverService {
  constructor(
    private http: Http,
    private encodingService: EncodingService,
    private notificationService: NotificationService,
  ) { }

  getBranchInfoList(id: number, workspace: string): Observable<BranchInfo[]> {
    return new Observable(observer => {
      // Create diff files request
      const diffFilesRequest: DiffFilesRequest = new DiffFilesRequest();
      diffFilesRequest.setDiffId(id);
      diffFilesRequest.setWorkspace(workspace);
      const requestBinary: Uint8Array = diffFilesRequest.serializeBinary();
      const requestBase64: string = this.encodingService
        .encodeUint8ArrayToBase64String(requestBinary);

      // Send the request to local server
      this.http
        .get('http://localhost:7000/get_diff_files?request=' + requestBase64)
        .map(response => response.text())
        .subscribe(diffFilesResponseBase64 => {
          // Decode response
          const diffFilesResponseBinary: Uint8Array = this.encodingService
            .decodeBase64StringToUint8Array(diffFilesResponseBase64);
          const diffFilesResponse: DiffFilesResponse = DiffFilesResponse
            .deserializeBinary(diffFilesResponseBinary);

          if (diffFilesResponse.getBranchinfoList().length === 0) {
            observer.error();
            this.notificationService.error('Local server: Branches not found');
          }

          observer.next(diffFilesResponse.getBranchinfoList());
        }, () => {
          this.localserverError();
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
      const textDiffRequest: TextDiffRequest = new TextDiffRequest();
      textDiffRequest.setLeftFile(leftFile);
      textDiffRequest.setRightFile(rightFile);
      const requestBinary: Uint8Array = textDiffRequest.serializeBinary();
      const requestBase64: string = this.encodingService
        .encodeUint8ArrayToBase64String(requestBinary);

      // Send the request to local server
      this.http
        .get('http://localhost:7000/get_text_diff?request=' + requestBase64)
        .map(response => response.text())
        .subscribe(textDiffResponseBase64 => {
          // Decode response
          const textDiffResponseBinary: Uint8Array = this.encodingService
            .decodeBase64StringToUint8Array(textDiffResponseBase64);
          const textDiffResponse: TextDiffResponse = TextDiffResponse
            .deserializeBinary(textDiffResponseBinary);

          observer.next(textDiffResponse);
        }, () => {
          this.localserverError();
          observer.error();
        });
    });
  }

  // Get all newest files of a diff by id and workspace
  getDiffFiles(id: number, workspace: string): Observable<File[]> {
    return new Observable(observer => {
      this.getBranchInfoList(id, workspace).subscribe(branchInfoList => {
        let files: File[] = [];
        for (const branchInfo of branchInfoList) {
          const branchFiles: File[] = this.getFilesFromBranchInfo(branchInfo);
          files = files.concat(branchFiles);
        }
        observer.next(files);
      });
    });
  }

  // Get only newest instances of files from the branch
  getFilesFromBranchInfo(branchInfo: BranchInfo): File[] {
    const fileDictionary: { [filename: string]: File } = {};
    for (const commit of branchInfo.getCommitList()) {
      this.addWithReplace(fileDictionary, commit.getFileList());
    }
    this.addWithReplace(fileDictionary, branchInfo.getUncommittedFileList());

    return Object.values(fileDictionary);
  }

  private addWithReplace(
    fileDictionary: { [filename: string]: File },
    newfiles: File[],
  ): void {
    for (const file of newfiles) {
      // If file with the filename is already exist, it will be replaced by
      // the new one.
      fileDictionary[file.getFilenameWithRepo()] = file;
    }
  }

  // Get file and branchInfo from branchInfo list by filename
  getFileData(
    filenameWithRepo: string,
    branchInfoList: BranchInfo[],
  ): FileData {
    for (const branchInfo of branchInfoList) {
      const files: File[] = this.getFilesFromBranchInfo(branchInfo);
      for (const file of files) {
        if (file.getFilenameWithRepo() === filenameWithRepo) {
          // File found
          return {
            branchInfo: branchInfo,
            file: file,
          };
        }
      }
    }

    // File not found
    throw new Error('File not found');
  }

  getCommitIdList(filenameWithRepo: string, branchInfo: BranchInfo): string[] {
    const commitIdList: string[] = [];

    // Add HEAD commit id
    const headCommitId: string = branchInfo.getCommitList()[0].getId();
    commitIdList.push(headCommitId);

    // Add all commit ids, where the file is present
    for (const commit of branchInfo.getCommitList()) {
      for (const file of commit.getFileList()) {
        if (file.getFilenameWithRepo() === filenameWithRepo) {
          commitIdList.push(commit.getId());
        }
      }
    }

    return commitIdList;
  }

  localserverError(): void {
    this.notificationService.error("Local server doesn't respond");
  }
}
