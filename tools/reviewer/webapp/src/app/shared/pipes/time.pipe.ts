import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';

// Angular date format guide:
// https://angular.io/api/common/DatePipe#pre-defined-format-options

@Pipe({
  name: 'time',
})
export class TimePipe implements PipeTransform {
  transform(timestamp: number, timeFormat: string): string {
    const datePipe = new DatePipe('en-US');

    if (timeFormat === 'fullDate') {
      return datePipe.transform(timestamp, 'h:mm a, MMM dd, yyyy z');
    } else {
      if (this.isToday(timestamp)) {
        // It's today
        return datePipe.transform(timestamp, 'h:mm a');
      } else {
        // Several days ago or more
        return datePipe.transform(timestamp, 'MMM dd, yyyy');
      }
    }
  }

  isToday(timestamp: number): boolean {
    const datePipe = new DatePipe('en-US');
    const dateFormat: string = 'd.M.yyyy';

    // Date string of right now
    const todayDate: string = datePipe.transform(Date.now(), dateFormat);
    // Date string of the value, which we need to display
    const pipedDate: string = datePipe.transform(timestamp, dateFormat);

    return todayDate === pipedDate;
  }
}
