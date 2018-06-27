import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';

import { Diff } from '@/shared/shell';

// The service provides communication between review components.
@Injectable()
export class ReviewService {
  diffChanges = new Subject<Diff>();

  // Tell other components, that diff was changed.
  saveLocalDiff(diff: Diff): void {
    this.diffChanges.next(diff);
  }
}
