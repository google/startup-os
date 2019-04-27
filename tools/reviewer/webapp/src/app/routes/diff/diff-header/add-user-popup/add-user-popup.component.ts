import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { Subscription } from 'rxjs';

import { DocumentEventService } from '@/core/services';

@Component({
  selector: 'add-user-popup',
  templateUrl: './add-user-popup.component.html',
  styleUrls: ['./add-user-popup.component.scss'],
})
export class AddUserPopupComponent implements OnDestroy {
  isEmailInvalid: boolean;
  email: string;
  isVisible: boolean = false;
  keySubscription = new Subscription();

  @Input() placeholder: string;
  @Input() validCheck: boolean;
  @Output() returnEmailEmitter = new EventEmitter<string>();

  constructor(private documentEventService: DocumentEventService) {
    this.reset();
  }

  open(): void {
    this.subscribeOnKeydown();
    this.isVisible = true;
  }

  close(): void {
    this.keySubscription.unsubscribe();
    this.reset();
  }

  add(): void {
    if (!this.validCheck || this.isEmailValid(this.email)) {
      this.returnEmailEmitter.emit(this.email);
      this.close();
    } else {
      this.isEmailInvalid = true;
    }
  }

  isEmailValid(email: string): boolean {
    if (!email) {
      return false;
    }
    // <something>@<something>
    // e.g. "username@domain.com"
    const emailValidation: RegExp = /^.+?@.+?$/;
    return emailValidation.test(email);
  }

  hideError(): void {
    this.isEmailInvalid = false;
  }

  reset(): void {
    this.email = '';
    this.isEmailInvalid = false;
    this.isVisible = false;
  }

  private subscribeOnKeydown(): void {
    this.keySubscription = this.documentEventService.keydown.subscribe((event: KeyboardEvent) => {
      // Hide error when user is typing something
      this.hideError();

      // If Enter is pressed then add user
      if (event.key === 'Enter') {
        this.add();
      }

      // If Esc is pressed then close popup
      if (event.key === 'Escape') {
        this.close();
      }
    });
  }

  ngOnDestroy() {
    this.keySubscription.unsubscribe();
  }
}
