import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material';

export enum NotifierStatus {
  success = 'cr-snack-success',
  info = 'cr-snack-info',
  warning = 'cr-snack-warning',
  error = 'cr-snack-error',
}

@Injectable()
export class NotificationService {
  private duration: number;

  constructor(private snackbar: MatSnackBar) {
    this.duration = 4500;
  }

  snack(
    message: string,
    action: string = '',
    duration: number = null,
    status: NotifierStatus,
  ): void {
    if (!message) {
      return;
    }

    this.snackbar.open(message, action, {
      duration: duration ? duration : this.duration,
      panelClass: [status],
    });
  }

  success(
    message: string,
    action: string = '',
    duration: number = null,
  ): void {
    this.snack(message, action, duration, NotifierStatus.success);
  }

  info(message: string, action: string = '', duration: number = null): void {
    this.snack(message, action, duration, NotifierStatus.info);
  }

  warning(
    message: string,
    action: string = '',
    duration: number = null,
  ): void {
    this.snack(message, action, duration, NotifierStatus.warning);
  }

  error(message: string, action: string = '', duration: number = null): void {
    this.snack(message, action, duration, NotifierStatus.error);
  }
}
