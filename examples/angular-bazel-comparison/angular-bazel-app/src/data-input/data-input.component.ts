import { Component, OnInit } from '@angular/core';

import { MockService } from '../services/mock.service';

@Component({
  selector: 'data-input',
  templateUrl: './data-input.component.html',
  styleUrls: ['./data-input.component.css']
})
export class DataInputComponent implements OnInit {
  inputData: string = 'aa';

  constructor(private mockService: MockService) { }

  ngOnInit() {
    this.clear();
  }

  save(): void {
    this.mockService.setData(this.inputData);
  }

  clear(): void {
    this.inputData = this.mockService.getData();
  }
}
