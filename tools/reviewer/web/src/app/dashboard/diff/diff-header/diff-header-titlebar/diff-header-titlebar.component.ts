import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Diff } from '@/shared/proto';
import {
  AuthService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { DiffHeaderService } from '../diff-header.service';

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
    public authService: AuthService,
    public firebaseService: FirebaseService,
    public notificationService: NotificationService,
    public diffHeaderService: DiffHeaderService,
  ) { }

  getAuthor(): string {
    return this.authService.getUsername(this.diff.getAuthor().getEmail());
  }

  changeAttention(): void {
    this.diffHeaderService.changeAttention(this.diff.getAuthor());

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const message: string = this.diff.getAuthor().getNeedsAttention() ?
        'Attention of author is requested' :
        'Attention of author is canceled';
      this.notificationService.success(message);
    }, () => {
      this.notificationService.error("Attention isn't changed");
    });
  }

  clickReplyButton() {
    this.toggleReplyPopup.emit(true);
  }
}
