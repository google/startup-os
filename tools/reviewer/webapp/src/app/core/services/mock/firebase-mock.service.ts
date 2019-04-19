import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { Diff } from '@/core/proto';

@Injectable()
export class FirebaseMockService {
  getDiffs(): Observable<Diff[]> {
    return of([new Diff()]);
  }

  getDiff(id: string): Observable<Diff> {
    return of(new Diff());
  }

  updateDiff(diff: Diff): Observable<void> {
    return of();
  }

  removeDiff(id: string): Observable<void> {
    return of();
  }
}
