import { Component, EventEmitter, Input, Output } from '@angular/core';

// Button interface to delete/edit a comment
@Component({
  selector: 'comment-menu',
  templateUrl: './comment-menu.component.html',
  styleUrls: ['./comment-menu.component.scss'],
})
export class CommentMenuComponent {
  @Input() isMenuVisible: boolean = false;
  @Output() editEmitter = new EventEmitter<void>();
  @Output() deleteEmitter = new EventEmitter<void>();
  @Output() toggleEmitter = new EventEmitter<boolean>();

  toggle(): void {
    this.isMenuVisible = !this.isMenuVisible;
    this.toggleEmitter.emit(this.isMenuVisible);
  }

  edit(): void {
    this.toggle();
    this.editEmitter.emit();
  }

  delete(): void {
    this.toggle();
    this.deleteEmitter.emit();
  }
}
