import { Injectable } from '@angular/core';
import { Observable, Subject, Subscription } from 'rxjs';
import { of } from 'rxjs/observable/of';

import { Diff } from '@/core/proto';
import { FirebaseService } from './firebase.service';

@Injectable()
export class FirebaseStateService {
  private diff: Diff;
  private diffs: Diff[] = [];
  diffChanges = new Subject<Diff>();
  diffsChanges = new Subject<Diff[]>();
  isDiffLoading: boolean = false;
  isDiffsLoading: boolean = false;
  private diffSubscription = new Subscription();
  private diffsSubscription = new Subscription();

  constructor(private firebaseService: FirebaseService) { }

  getDiff(id: string): Observable<Diff> {
    this.connectDiff(id);
    if (!this.isDiffLoading) {
      // Return loaded diff
      return of(this.diff);
    } else {
      // Diff isn't loaded yet.
      // Wait until loaded then return.
      return new Observable(observer => {
        const subscription: Subscription = this.diffChanges.subscribe(diff => {
          subscription.unsubscribe();
          observer.next(diff);
        });
      });
    }
  }

  getDiffs(): Observable<Diff[]> {
    if (!this.isDiffsLoading) {
      // Return loaded diffs
      return of(this.diffs);
    } else {
      // Diffs aren't loaded yet.
      // Wait until loaded then return.
      return new Observable(observer => {
        const subscription: Subscription = this.diffsChanges.subscribe(diffs => {
          subscription.unsubscribe();
          observer.next(diffs);
        });
      });
    }
  }

  private connectDiff(id: string): void {
    if (!this.diff || id !== this.diff.getId().toString()) {
      this.isDiffLoading = true;
      this.diffSubscription.unsubscribe();
      this.diffSubscription = this.firebaseService.getDiff(id).subscribe(diff => {
        this.diff = diff;
        this.isDiffLoading = false;
        this.diffChanges.next(diff);
      });
    }
  }

  connectDiffs(): void {
    if (this.diffs.length === 0) {
      this.isDiffsLoading = true;
      this.diffsSubscription.unsubscribe();
      this.diffsSubscription = this.firebaseService.getDiffs().subscribe(diffs => {
        this.diffs = diffs;
        this.isDiffsLoading = false;
        this.diffsChanges.next(diffs);
      });
    }
  }

  destroy(): void {
    this.diffSubscription.unsubscribe();
    this.diffsSubscription.unsubscribe();
  }
}
