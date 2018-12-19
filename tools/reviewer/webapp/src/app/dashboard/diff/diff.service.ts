import { Injectable } from '@angular/core';
import { Params, Router } from '@angular/router';

import { File } from '@/core/proto';

@Injectable()
export class DiffService {
  constructor(private router: Router) { }

  openFile(
    isNewTab: boolean,
    file: File,
    diffId: number,
    commitId?: string,
  ): void {
    const url: string = 'diff/' + diffId + '/' + file.getFilenameWithRepo();
    const queryParams: Params = { right: commitId };

    if (isNewTab) {
      // Open in new tab
      let newTabUrl: string = url;
      if (commitId) {
        newTabUrl += '?right=' + commitId;
      }
      window.open(newTabUrl, '_blank');
    } else {
      // Navigate to
      this.router.navigate([url], { queryParams: queryParams });
    }
  }
}
