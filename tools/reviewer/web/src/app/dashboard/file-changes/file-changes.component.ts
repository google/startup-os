import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { FileChangesService } from './file-changes.service';

@Component({
  selector: 'file-changes',
  templateUrl: './file-changes.component.html',
  styleUrls: ['./file-changes.component.scss'],
  providers: [FileChangesService],
})
export class FileChangesComponent implements OnInit {
  language: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private fileChangesService: FileChangesService,
  ) { }

  ngOnInit() {
    this.getUrlParam();
  }

  // Get parameters from url
  getUrlParam(): void {
    const filename: string = this.activatedRoute.snapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    const diffId = this.activatedRoute.snapshot.url[0].path;

    this.language = this.fileChangesService.getLanguage(filename);
    this.fileChangesService.startLoading(filename, diffId);
  }
}
