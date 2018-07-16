import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { FirebaseService, LocalserverService } from '@/shared/services';
import { Diff, File } from '@/shared/shell';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss'],
})
export class ReviewComponent implements OnInit {
  isLoading: boolean = true;
  diff: Diff;
  files: File[];
  // Show editable button next to fields
  isEditable: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private firebaseService: FirebaseService,
    private localserverService: LocalserverService,
  ) { }

  ngOnInit() {
    const diffId: string = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(diffId).subscribe(diff => {
      this.diff = diff;
      this.isEditable = this.getIsEditable(this.diff.getStatus());
      this.localserverService
        .getDiffFiles(this.diff.getId(), this.diff.getWorkspace())
        .subscribe(files => {
          this.files = files
            .filter(file => file.getAction() !== File.Action.DELETE);
          this.isLoading = false;
        });
    });
  }

  getIsEditable(status: Diff.Status): boolean {
    // Render the fields un-editable if the current diff status
    // is in the list of notEditableStatus
    // Fields can not be edited if status is 'SUBMITTED' or 'REVERTED'
    const statuses: Diff.Status[] = [
      Diff.Status.SUBMITTED,
      Diff.Status.REVERTED,
    ];
    return !statuses.includes(status);
  }
}
