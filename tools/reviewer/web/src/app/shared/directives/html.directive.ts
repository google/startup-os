import {
  Directive,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';

// Sets the element's HTML
@Directive({
  selector: '[HTML]',
})
export class HtmlDirective implements OnChanges {
  @Input() HTML: string;

  constructor(
    private elementRef: ElementRef,
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if ('HTML' in changes) {
      this.elementRef.nativeElement.innerHTML = this.HTML;
    }
  }
}
