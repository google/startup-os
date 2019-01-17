import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { CiResponse, Commit, Diff, Reviewer } from '@/core/proto';
import {
  DiffUpdateService,
  HighlightService,
  LocalserverService,
  UserService,
} from '@/core/services';
import { DiffHeaderService } from '../diff-header.service';

interface Status {
  message: string;
  color: string;
}

// The component implements content of the header
// How it looks: https://i.imgur.com/TgqzvTW.jpg
@Component({
  selector: 'diff-header-content',
  templateUrl: './diff-header-content.component.html',
  styleUrls: ['./diff-header-content.component.scss'],
  providers: [DiffHeaderService],
})
export class DiffHeaderContentComponent implements OnChanges, OnInit {
  description: string = '';
  isDescriptionEditMode: boolean = false;
  status: Status = this.getStatus(CiResponse.TargetResult.Status.NONE);

  @Input() diff: Diff;

  constructor(
    public userService: UserService,
    public diffUpdateService: DiffUpdateService,
    public diffHeaderService: DiffHeaderService,
    public highlightService: HighlightService,
    public localserverService: LocalserverService,
  ) { }

  ngOnChanges() {
    this.description = this.diff.getDescription();
  }

  ngOnInit() {
    // If CI exists then convert it to UI status
    const ci: CiResponse = this.diff.getCiResponseList()[0];
    if (ci) {
      const results: CiResponse.TargetResult[] = ci.getResultList();
      this.setStatus(results[results.length - 1]);
    }
  }

  // Loads commits list from localserver to compare it with CI result
  private setStatus(result: CiResponse.TargetResult): void {
    // Get branchInfoList from localserver
    this.localserverService
      .getBranchInfoList(
        this.diff.getId(),
        this.diff.getWorkspace(),
      )
      .subscribe(branchInfoList => {
        // Find branchInfo by repo id
        for (const branchInfo of branchInfoList) {
          if (branchInfo.getRepoId() === result.getTarget().getRepo().getId()) {
            const commits: Commit[] = branchInfo.getCommitList();
            // Take last commit (newest change)
            const commitId: string = commits[commits.length - 1].getId();

            // Set status from the result,
            // or set outdated status if result with the commit not found.
            this.status = (commitId === result.getTarget().getCommitId()) ?
              this.getStatus(result.getStatus()) :
              this.getStatus(CiResponse.TargetResult.Status.OUTDATED);
          }
        }
      });
  }

  // Converts enum to UI status
  getStatus(status: CiResponse.TargetResult.Status): Status {
    switch (status) {
      case CiResponse.TargetResult.Status.SUCCESS:
        return { message: 'Passed', color: '#12a736' };
      case CiResponse.TargetResult.Status.FAIL:
        return { message: 'Failed', color: '#db4040' };
      case CiResponse.TargetResult.Status.RUNNING:
        return { message: 'Running', color: '#1545bd' };
      case CiResponse.TargetResult.Status.OUTDATED:
        return { message: 'Outdated', color: '#808080' };
      default:
        return { message: '', color: '' };
    }
  }

  changeAttention(email: string): void {
    // Get reviewer
    const reviewer: Reviewer = this.diffHeaderService.getReviewer(
      this.diff,
      email,
    );
    if (!reviewer) {
      throw new Error('Reviewer not found');
    }

    // Change attention of the reviewer
    this.diffHeaderService.changeAttention(reviewer);

    // Send changes to firebase
    const username: string = this.userService.getUsername(reviewer.getEmail());
    this.diffUpdateService.updateAttention(this.diff, username);
  }

  // Is the email present in the CC list?
  isCcAlreadyPresent(diff: Diff, email: string): boolean {
    for (const cc of diff.getCcList()) {
      if (cc === email) {
        return true;
      }
    }
    return false;
  }

  addReviewer(email: string): void {
    if (email && !this.diffHeaderService.getReviewer(this.diff, email)) {
      const reviewer = new Reviewer();
      reviewer.setEmail(email);
      reviewer.setNeedsAttention(true);
      reviewer.setApproved(false);
      this.diff.addReviewer(reviewer);
      this.saveUserToFirebase(email);
    }
  }

  addCC(email: string): void {
    if (email && !this.isCcAlreadyPresent(this.diff, email)) {
      this.diff.addCc(email);
      this.saveUserToFirebase(email);
    }
  }

  addIssue(issue: string): void {
    this.diff.addIssue(issue);
    this.diffUpdateService.updateIssueList(this.diff);
  }

  removeIssue(issueToRemove: string): void {
    const issues: string[] = this.diff.getIssueList().filter(issue => issue !== issueToRemove);
    this.diff.setIssueList(issues);
    this.diffUpdateService.updateIssueList(this.diff);
  }

  getIssueNumber(issue: string): string {
    // 'https://github.com/google/startup-os/issues/317' -> [url, '317']
    const urlMatchArray: RegExpMatchArray = issue.match(/^.+?\/(\d+)$/);
    if (urlMatchArray && urlMatchArray[1]) {
      return urlMatchArray[1];
    } else {
      return 'Not Found';
    }
  }

  removeFromReviewerList(email: string): void {
    this.diffHeaderService.removeFromReviewerList(this.diff, email);
    this.removeUserFromFirebase(email);
  }

  removeFromCcList(email: string): void {
    this.diffHeaderService.removeFromCcList(this.diff, email);
    this.removeUserFromFirebase(email);
  }

  saveUserToFirebase(email: string): void {
    this.updateUserListInFirebase(email, 'added');
  }

  removeUserFromFirebase(email: string): void {
    this.updateUserListInFirebase(email, 'removed');
  }

  updateUserListInFirebase(email: string, action: string): void {
    const username: string = this.userService.getUsername(email);
    this.diffUpdateService.customUpdate(this.diff, username + ' is ' + action);
  }

  startDescriptionEditMode(): void {
    this.isDescriptionEditMode = true;
  }

  stopDescriptionEditMode(): void {
    this.description = this.diff.getDescription();
    this.isDescriptionEditMode = false;
  }

  // Get description, where urls are highlighted as a link, and html escaped
  getParsedDescription(): string {
    // The RegExp example is taken from here:
    // https://stackoverflow.com/a/3809435

    // tslint:disable-next-line
    const urlRegExp: RegExp = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/g;

    // We need to escape special html chars, but a url contains some of the special chars.
    // So we need to highlight urls and escape special chars separately.
    // Implementation: find a url, highlight it, make all text before the url escaped,
    // go to the next url.
    let parsedDescription: string = '';
    let clippedDescription: string = this.description;

    // Get all urls in the description
    const urls: RegExpMatchArray = this.description.match(urlRegExp);
    if (urls) {
      for (const url of urls) {
        const linkIndex: number = clippedDescription.search(urlRegExp);
        // All text before the url
        const commonText: string = clippedDescription.substr(0, linkIndex);
        parsedDescription +=
          this.highlightService.htmlSpecialChars(commonText) + // Escape html
          url.link(url); // Hightlight the url
        // Cut the part, which we already parsed
        clippedDescription = clippedDescription.substr(
          linkIndex + url.length,
          clippedDescription.length - (linkIndex + url.length),
        );
      }
      // Don't forget about text after last found url
      parsedDescription += this.highlightService.htmlSpecialChars(clippedDescription);

      return parsedDescription;
    } else {
      // Description doesn't contain any links

      // Return a placeholder, if description is empty
      const placeholder: string = 'Description...';
      return this.description ? this.description : placeholder;
    }
  }

  saveDescription(): void {
    this.diff.setDescription(this.description);
    this.diffUpdateService.updateDescription(this.diff);
    this.isDescriptionEditMode = false;
  }
}
