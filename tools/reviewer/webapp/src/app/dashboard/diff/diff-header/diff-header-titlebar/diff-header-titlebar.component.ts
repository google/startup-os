import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Diff } from '@/core/proto';
import { AuthService, DiffUpdateService } from '@/core/services';
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
    public authService: AuthService,
    public diffUpdateService: DiffUpdateService,
    public diffHeaderService: DiffHeaderService,
  ) { }

  getAuthor(): string {
    return this.authService.getUsername(this.diff.getAuthor().getEmail());
  }

  changeAttention(): void {
    this.diffHeaderService.changeAttention(this.diff.getAuthor());
    this.diffUpdateService.updateAttention(this.diff, 'author');
  }

  clickReplyButton() {
    this.toggleReplyPopup.emit(true);
  }
}
