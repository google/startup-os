import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { FirebaseService } from '@/shared/services';
import { Diff, File } from '@/shared/shell';
import { MockServerService } from '../diff/mock-server.service';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss'],
  providers: [MockServerService],
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
    private mockServerService: MockServerService,
  ) { }

  ngOnInit() {
    const diffId: string = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(diffId).subscribe(diff => {
      this.diff = diff;
      this.fileList = this.mockServerService.getMockFiles(
        this.diff.getWorkspace(),
      );
      this.isEditable = this.getIsEditable(this.diff.getStatus());
      this.isLoading = false;
    }, () => {
      // Permission Denied
    });
  }

  getIsEditable(status: Diff.Status): boolean {
    // Render the fields un-editable if the current diff status
    // is in the list of notEditableStatus
    // Fields can not be edited if status is 'SUBMITTED' or 'REVERTED'
    const statuses = [
      Diff.Status.SUBMITTED,
      Diff.Status.REVERTED,
    ];
    return !statuses.includes(status);
  }
}
