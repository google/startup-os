import { Component, Input } from '@angular/core';

@Component({
  selector: 'page-loading',
  templateUrl: './page-loading.component.html',
  styleUrls: ['./page-loading.component.scss'],
})
export class PageLoadingComponent {
  @Input() isLoading: boolean;
}
