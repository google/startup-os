import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { FirebaseService } from '@/shared/services';
import { Diff, File } from '@/shared/shell';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss'],
})
export class ReviewComponent implements OnInit {
  isLoading: boolean = true;
  diff: Diff;
  fileList: File[];
  // Show editable button next to fields
  isEditable: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private firebaseService: FirebaseService,
  ) { }

  ngOnInit() {
    const diffId: string = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(diffId).subscribe(diff => {
      this.diff = diff;
      this.fileList = this.getFiles(this.diff.getWorkspace());
      this.isEditable = this.getIsEditable(this.diff.getStatus());
      this.isLoading = false;
    }, () => {
      // Permission Denied
    });
  }

  getFiles(workspace: string): File[] {
    // Hardcoded files
    // We have mock diffs in firebase, so localserver can't provide
    // any files for the diffs
    // TODO: use localserver response instead
    const filenames = [
      'aa/aa_commands.py',
      'review_server/local_firebase.py',
      'aa/aa_tool.py',
    ];
    const fileList: File[] = [];
    for (const filename of filenames) {
      const file = new File();
      file.setFilename(filename);
      file.setRepoId('startup-os');
      file.setWorkspace(workspace);
      fileList.push(file);
    }

    return fileList;
  }

  getIsEditable(status: Diff.Status): boolean {
    // Render the fields un-editable if the current diff status
    // is in the list of notEditableStatus
    // Fields can not be edited if status is 'SUBMITTED' or 'REVERTED'
    const statuses = [
      Diff.Status.SUBMITTED,
      Diff.Status.REVERTED
    ];
    return !statuses.includes(status);
  }
}
