import { inject, TestBed } from '@angular/core/testing';

import { DifferenceService } from './difference.service';

describe('DifferenceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DifferenceService]
    });
  });

  it(
    'should be created',
    inject([DifferenceService], (service: DifferenceService) => {
      expect(service).toBeTruthy();
    })
  );
});
