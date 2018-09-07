import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'time',
})
export class TimePipe implements PipeTransform {
  transform(timestamp: number): string {
    // Date of right now
    const currentDate: Date = new Date(Date.now());
    // Date of the value, which we need to display
    const pipedDate: Date = new Date(timestamp);

    if (this.getDateString(pipedDate) === this.getDateString(currentDate)) {
      // It's today.
      // Display time only
      return this.getTimeString(pipedDate);
    } else {
      // Several days ago or more.
      // Display date only.
      return this.getDateString(pipedDate);
    }
  }

  // Add zero, if number < 10;
  // Example:
  // 12 -> 12
  // 5 -> 05
  // 29 -> 29
  // 0 -> 00
  addZero(integer: number): string {
    return ('0' + integer).slice(-2);
  }

  // Date -> '09:57'
  getTimeString(date: Date): string {
    return this.addZero(date.getHours()) + ':' + this.addZero(date.getMinutes());
  }

  // Date -> '5 sep 2018'
  getDateString(date: Date): string {
    const monthNames: string[] = [
      'jan',
      'feb',
      'mar',
      'apr',
      'may',
      'jun',
      'jul',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
    ];

    return date.getDate() + ' ' +
      monthNames[date.getMonth()] + ' ' +
      date.getFullYear();
  }
}
