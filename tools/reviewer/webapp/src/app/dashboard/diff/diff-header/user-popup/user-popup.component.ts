import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

// Popup, which can be seen, when a username is hovered.
// It provides an ability to add/remove a user from user list,
// and change attention of the user.
// Also displays email of the user.
// How it looks: https://i.imgur.com/3zf9Dpu.jpg
@Component({
  selector: 'user-popup',
  templateUrl: './user-popup.component.html',
  styleUrls: ['./user-popup.component.scss'],
})
export class UserPopupComponent {
  isHovered: boolean = false;

  @Input() email: string;
  @Input() isChangeAttention: boolean;
  @Input() isRemoveFromList: boolean;
  @Input() isAttentionRequired: boolean;
  @Output() changeAttentionEmitter = new EventEmitter();
  @Output() removeFromListEmitter = new EventEmitter();

  @HostListener('mouseenter')
  mouseenter() {
    this.isHovered = true;
  }

  @HostListener('mouseleave')
  mouseleave() {
    this.close();
  }

  close(): void {
    this.isHovered = false;
  }

  changeAttention(): void {
    this.changeAttentionEmitter.emit(this.email);
    this.close();
  }

  removeFromList(): void {
    this.removeFromListEmitter.emit(this.email);
    this.close();
  }
}
