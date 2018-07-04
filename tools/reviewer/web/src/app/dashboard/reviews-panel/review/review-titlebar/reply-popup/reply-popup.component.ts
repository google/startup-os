import { Diff } from '@/shared';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'cr-reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss']
})
export class ReplyPopupComponent implements OnInit {
  @Input() diff: Diff.AsObject;
  actionRequired = false;
  approved = false;
  constructor() { }

  ngOnInit() {
  }

}
