import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

// Popup, which can be seen, when a user is hovered.
// It provides an ability to add/remove a user from user list,
// and change attention of the user.
// Also displays email of the user.
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
    this.isHovered = false;
  }

  close() {
    this.isHovered = false;
  }

  changeAttention() {
    this.changeAttentionEmitter.emit(this.email);
    this.close();
  }

  removeFromList() {
    this.removeFromListEmitter.emit(this.email);
    this.close();
  }
}
