import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Diff } from '@/core/proto';
import { DiffUpdateService, NotificationService, UserService } from '@/core/services';
import { DiffHeaderService } from '../diff-header.service';

// The component implements titlebar of the header
// How it looks: https://i.imgur.com/vVmTgnq.jpg
@Component({
  selector: 'diff-header-titlebar',
  templateUrl: './diff-header-titlebar.component.html',
  styleUrls: ['./diff-header-titlebar.component.scss'],
  providers: [DiffHeaderService],
})
export class DiffHeaderTitlebarComponent {
  @Input() diff: Diff;
  @Input() isReplyPopup: boolean;
  @Output() toggleReplyPopup = new EventEmitter<boolean>();

  constructor(
    public userService: UserService,
    public diffUpdateService: DiffUpdateService,
    public diffHeaderService: DiffHeaderService,
    public notificationService: NotificationService,
  ) { }

  getAuthor(): string {
    return this.userService.getUsername(this.diff.getAuthor().getEmail());
  }

  // Is current user an author of the diff
  isAuthor(): boolean {
    return this.userService.email === this.diff.getAuthor().getEmail();
  }

  isDraft(): boolean {
    return this.diff.getStatus() === Diff.Status.REVIEW_NOT_STARTED;
  }

  changeAttention(): void {
    this.diffHeaderService.changeAttention(this.diff.getAuthor());
    this.diffUpdateService.updateAttention(this.diff, 'author');
  }

  clickReplyButton() {
    this.toggleReplyPopup.emit(true);
  }

  startReview(): void {
    if (this.diff.getReviewerList().length > 0) {
      this.diff.setStatus(Diff.Status.UNDER_REVIEW);
      this.diffUpdateService.customUpdate(this.diff, 'Review is started');
    } else {
      this.notificationService.error('There must be at least one reviewer to start review');
    }
  }

  submit(): void {
    // TODO: add some functionality
    this.notificationService.warning('The functionality is not implemented yet :)');
  }
}
