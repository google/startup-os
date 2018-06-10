import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService,
  Status
} from '@/shared';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit {
  diffId: string;
  diff: Diff;

  // Show editable button next to fields
  editable: boolean = true;
  // Fields can not be edited if status is 'SUBMITTED' or 'REVERTED'
  notEditableStatus: Array<number> = [Status.SUBMITTED, Status.REVERTED];

  constructor(
    private route: ActivatedRoute,
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.diffId = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(this.diffId).subscribe(
      res => {
        // Create Diff from proto
        this.protoService.open.subscribe(error => {
          if (error) {
            throw error;
          }
          const review = res;
          this.diff = this.protoService.createDiff(review);
          this.diff.number = parseInt(this.diffId, 10);
          // Render the fields un-editable if the current diff status
          // is in the list of notEditableStatus
          this.editable = !this.notEditableStatus.includes(this.diff.status);
        });
      },
      () => {
        // Permission Denied
      }
    );
  }

  // Upon click on a file open a single file review page showing
  // code difference and comments
  openFile(filePosition): void {
    // Build a route path on the following format /diff/<diff number>/<path>?
    // ls=<left snapshot number>&rs=<right snapshot number>
    this.router.navigate(['diff/' + this.diffId + '/' + filePosition], {
      queryParams: { ls: '1', rs: '3' }
    });
  }

  // Save needAttentionOf list and update the Diff
  saveAttentionList(name: string): void {
    if (this.diff.needAttentionOf.includes(name)) {
      this.diff.needAttentionOf = this.diff.needAttentionOf.filter(
        e => e !== name
      );
    } else {
      this.diff.needAttentionOf.push(name);
    }
    this.updateDiff(this.diff, 'Update Need Attention List');
  }

  // Remove a property from Diff and update the Diff
  removeProperty(property: string, message: string): void {
    this.firebaseService
      .removeProperty(this.diff, property)
      .then(() => {
        this.notificationService.success(message);
      })
      .catch(() => {
        this.notificationService.error('Some error occured');
      });
  }

  // Update the Diff in the DB
  updateDiff(diff: Diff, message: string): void {
    this.firebaseService.updateDiff(diff).subscribe(
      () => {
        this.notificationService.success(message);
      },
      () => {
        this.notificationService.error('Some error occured');
      }
    );
  }
}
