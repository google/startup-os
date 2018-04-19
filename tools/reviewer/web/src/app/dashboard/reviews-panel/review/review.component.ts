import { Diff, FirebaseService, ProtoService } from '@/shared';
import { Component, NgZone, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit {
  diffId: string;
  diff: Diff;
  constructor(
    private route: ActivatedRoute,
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private zone: NgZone,
    private router: Router
  ) {}

  ngOnInit() {
    this.diffId = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(this.diffId).subscribe(res => {
      // Create Diff from proto
      this.protoService.open.subscribe(error => {
        if (error) {
          throw error;
        }
        const review = res;
        this.diff = this.protoService.createDiff(review);
        this.diff.number = parseInt(this.diffId, 10);
      });
    });
  }

  openFile(filePosition): void {
    this.zone.run(() => {
      // Build a route path on the following format
      // /diff/<diff number>/<path>?
      // ls=<left snapshot number>&rs=<right snapshot number>
      this.router.navigate(['diff/' + this.diffId + '/' + filePosition], {
        queryParams: { ls: '1', rs: '3' }
      });
    });
  }
}
