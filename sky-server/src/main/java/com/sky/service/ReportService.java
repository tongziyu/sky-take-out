package com.sky.service;

import javax.servlet.http.HttpServletResponse;

public interface ReportService {

    /**
     * 导出近30天的运营数据报表
     * @param response
     **/
    void exportBusinessData(HttpServletResponse response);
}
