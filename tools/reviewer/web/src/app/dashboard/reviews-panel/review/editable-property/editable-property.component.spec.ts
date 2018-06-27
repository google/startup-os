import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditablePropertyComponent } from './editable-property.component';

describe('EditablePropertyComponent', () => {
  let component: EditablePropertyComponent;
  let fixture: ComponentFixture<EditablePropertyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EditablePropertyComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditablePropertyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
