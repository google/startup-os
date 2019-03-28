import {
  Directive,
  ElementRef,
  Input,
  OnInit,
} from '@angular/core';

// Focuses an element
@Directive({
  selector: '[focus]',
})
export class FocusDirective implements OnInit {
  @Input('focus') isFocused: boolean;

  constructor(private hostElement: ElementRef) { }

  ngOnInit() {
    if (this.isFocused) {
      this.hostElement.nativeElement.focus();
    }
  }
}
