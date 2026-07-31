import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeePanel } from './employee-panel';

describe('EmployeePanel', () => {
  let component: EmployeePanel;
  let fixture: ComponentFixture<EmployeePanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeePanel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeePanel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
