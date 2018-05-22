import { AuthService, FirebaseService, Lists, ProtoService } from '@/shared';
import { Diff } from '@/shared/services/proto/messages';
import { ApplicationRef, Component, NgZone, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

@Component({
  selector: 'app-reviews-panel',
  templateUrl: './reviews-panel.component.html',
  styleUrls: ['./reviews-panel.component.scss']
})
export class ReviewsPanelComponent implements OnInit {
  diffs: Array<Array<Diff>> = [[], [], [], []];
  subscribers: any = {};
  username: string;
  constructor(
    private firebaseService: FirebaseService,
    private protoService: ProtoService,
    private appRef: ApplicationRef,
    private router: Router,
    private zone: NgZone,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Get username from url if there is no user.
    // Get the loggedIn username from its email.
    this.username = this.router.parseUrl(this.router.url).queryParams['user'];
    if (!this.username) {
      // Get user from AuthService
      this.authService.getUser().subscribe(user => {
        if (user) {
          const email = user.email;
          this.username = email.split('@')[0];
        }
      });
    }
    this.getReviews();
  }

  // Get diff/reviews from Database
  getReviews() {
    this.firebaseService.getDiffs().subscribe(
      res => {
        // Got the diffs as res
        this.subscribers.proto = this.protoService.open.subscribe(error => {
          if (error) {
            throw error;
          }

          // Diffs are categorized in 4 different lists

          this.diffs[Lists.NeedAttention] = [];
          this.diffs[Lists.CcedOn] = [];
          this.diffs[Lists.DraftReviews] = [];
          this.diffs[Lists.SubmittedReviews] = [];

          // Get diffIds from res; they are later used in routing
          const diffIds = Object.getOwnPropertyNames(res);

          // Iterate over each object in res
          // and create Diff from proto and categorize
          // into a specific list.

          Object.keys(res).map((value, index) => {
            res[value].number = diffIds[index];
            const diff = this.protoService.createDiff(res[value]);
            if (diff.needAttentionOf.includes(this.username)) {
              // Need attention of user
              this.diffs[Lists.NeedAttention].push(diff);
            } else if (diff.cc.includes(this.username)) {
              // User is cc'ed on this
              this.diffs[Lists.CcedOn].push(diff);
            } else if (diff.author === this.username && diff.status === 0) {
              // Draft Review
              this.diffs[Lists.DraftReviews].push(diff);
            } else if (diff.author === this.username && diff.status === 4) {
              // Submitted Review
              this.diffs[Lists.SubmittedReviews].push(diff);
            }
            // Trigger angular's change detection
            this.appRef.tick();
          });
          this.sortDiffs();
        });
      },
      () => {
        // Permission Denied
      }
    );
  }

  // Sort the diffs based on their date modified
  sortDiffs(): void {
    for (const list of this.diffs) {
      list.sort((a, b) => {
        return Math.sign(b.modifiedTimestamp - a.modifiedTimestamp);
      });
    }
  }

  diffClicked(diffId: Diff): void {
    // Navigate to display each Diff on another page
    this.zone.run(() => {
      this.router.navigate(['diff/', diffId]);
    });
  }
}
