import {
  Directive,
  ElementRef,
  Input,
  OnInit,
  Renderer,
} from '@angular/core';

// Focuses an element
@Directive({
  selector: '[focus]',
})
export class FocusDirective implements OnInit {
  @Input('focus') isFocused: boolean;

  constructor(
    private hostElement: ElementRef,
    private renderer: Renderer,
  ) { }

  ngOnInit() {
    if (this.isFocused) {
      this.renderer.invokeElementMethod(this.hostElement.nativeElement, 'focus');
    }
  }
}
