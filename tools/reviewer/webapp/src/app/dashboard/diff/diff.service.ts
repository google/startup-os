import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { File } from '@/shared/proto';

@Injectable()
export class DiffService {
  constructor(private router: Router) { }

  openFile(file: File, diffId: number): void {
    this.router.navigate([
      'diff/' + diffId + '/' + file.getFilenameWithRepo(),
    ]);
  }
}
