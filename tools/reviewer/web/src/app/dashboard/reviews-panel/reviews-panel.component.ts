import { AuthService, FirebaseService, Lists } from '@/shared';
import { Diff } from '@/shared/shell/proto/code-review_pb';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reviews-panel',
  templateUrl: './reviews-panel.component.html',
  styleUrls: ['./reviews-panel.component.scss']
})
export class ReviewsPanelComponent implements OnInit {
  diffs: Array<Array<Diff.AsObject>> = [[], [], [], []];
  subscribers: any = {};
  username: string;
  constructor(
    private firebaseService: FirebaseService,
    private router: Router,
    private authService: AuthService
  ) { }

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
          // TODO: Maybe to use email instead of username?
          // Also, we could keep users in separate collection
          // as a pair 'email - name', or something like that.
          // And to get access to a name by email.
        }
      });
    }
    this.getReviews();
  }

  // Get diff/reviews from Database
  getReviews() {
    this.firebaseService.getDiffs().subscribe(
      protoDiffs => {
        // Diffs are categorized in 4 different lists
        this.diffs[Lists.NeedAttention] = [];
        this.diffs[Lists.CcedOn] = [];
        this.diffs[Lists.DraftReviews] = [];
        this.diffs[Lists.SubmittedReviews] = [];

        // Iterate over each object in res
        // and create Diff from proto and categorize
        // into a specific list.
        for (const protoDiff of protoDiffs) {
          const diff: Diff.AsObject = protoDiff.toObject();
          if (diff.needAttentionOfList.includes(this.username)) {
            // Need attention of user
            this.diffs[Lists.NeedAttention].push(diff);
          } else if (diff.ccList.includes(this.username)) {
            // User is cc'ed on this
            this.diffs[Lists.CcedOn].push(diff);
          } else if (diff.author === this.username && diff.status === 0) {
            // Draft Review
            this.diffs[Lists.DraftReviews].push(diff);
          } else if (diff.author === this.username && diff.status === 4) {
            // Submitted Review
            this.diffs[Lists.SubmittedReviews].push(diff);
          }
        }

        this.sortDiffs();
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

  // Navigate to a Diff
  diffClicked(diffId: Diff): void {
    this.router.navigate(['diff/', diffId]);
  }
}
