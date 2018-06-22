import { Files } from '@/shared';
import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'review-files',
  templateUrl: './review-files.component.html',
  styleUrls: ['./review-files.component.scss']
})
export class ReviewFilesComponent {
  @Input() files: Files;
  @Input() diffId;

  constructor(private router: Router) {}

  // Upon click on a file open a single file review page showing
  // code difference and comments
  openFile(filePosition): void {
    // Build a route path on the following format /diff/<diff number>/<path>?
    // ls=<left snapshot number>&rs=<right snapshot number>
    this.router.navigate(['diff/' + this.diffId + '/' + filePosition], {
      queryParams: { ls: '1', rs: '3' }
    });
  }
}
