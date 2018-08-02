import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { File } from '@/shared/shell';

@Component({
  selector: 'review-files',
  templateUrl: './review-files.component.html',
  styleUrls: ['./review-files.component.scss'],
})
export class ReviewFilesComponent {
  isLoading: boolean = true;
  @Input() files: File[];
  @Input() diffId: number;

  constructor(private router: Router) { }

  ngOnInit() {
    this.isLoading = false;
  }

  // Upon click on a file open a single file review page showing
  // code difference and comments
  openFile(file: File): void {
    this.router.navigate(['diff/' + this.diffId + '/' + file.getFilename()]);
  }
}
