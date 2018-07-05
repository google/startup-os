import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewsPanelComponent } from './reviews-panel.component';

describe('ReviewsPanelComponent', () => {
  let component: ReviewsPanelComponent;
  let fixture: ComponentFixture<ReviewsPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReviewsPanelComponent ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReviewsPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
