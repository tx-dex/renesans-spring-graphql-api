package fi.sangre.renesans.controllers;

import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.service.CustomerService;
import fi.sangre.renesans.service.ExcelWriterService;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ExcelReport {

    @Autowired
    private ExcelWriterService excelWriterService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/survey/report/excel/{customerId}")
    @PreAuthorize("isAuthenticated()")
    public void downloadExcelFile(@PathVariable Long customerId, HttpServletResponse response) throws Exception {
        final Customer customer = customerService.getCustomer(customerId);
        try {
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"wecan -" + customer.getName() + ".xlsx\""));
            response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            excelWriterService.excelGenerator(customer, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(response.getOutputStream());
        }
    }
}
