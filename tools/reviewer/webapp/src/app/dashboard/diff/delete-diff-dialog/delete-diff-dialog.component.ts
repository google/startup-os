import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { Diff } from '@/shared/proto';
import { NotificationService } from '@/shared/services';

export interface DeleteDiffReturn {
  isDeleteDiff: boolean;
  isDeleteWorkspace: boolean;
}

@Component({
  selector: 'delete-diff-dialog',
  templateUrl: './delete-diff-dialog.component.html',
  styleUrls: ['./delete-diff-dialog.component.scss'],
})
export class DeleteDiffDialogComponent {
  diffId: number;
  isWorkspace: boolean;
  isError: boolean = false;

  constructor(
    private dialogRef: MatDialogRef<DeleteDiffDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private diff: Diff,
    private notificationService: NotificationService,
  ) { }

  close(): void {
    this.exit({
      isDeleteDiff: false,
      isDeleteWorkspace: false,
    });
  }

  delete(): void {
    if (this.diffId === this.diff.getId()) {
      // Entered diff id is correct. Close the dialog
      this.exit({
        isDeleteDiff: true,
        isDeleteWorkspace: this.isWorkspace,
      });
    } else {
      this.isError = true;
      if (!this.diffId) {
        this.notificationService.error('You must enter diff id to delete the diff');
      } else {
        this.notificationService.error('Diff id is incorrect');
      }
    }
  }

  // To set type of return
  private exit(deleteDiffReturn: DeleteDiffReturn): void {
    this.dialogRef.close(deleteDiffReturn);
  }

  // When user focuses input
  onFocus(): void {
    this.isError = false;
  }
}
