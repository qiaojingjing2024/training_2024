package org.qiaojingjing.collector;

import org.qiaojingjing.cons.Param;
import org.qiaojingjing.entity.Metric;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class CollectMem {
    private static final Map<String, Long> memMap = new HashMap<>();
    private static BigDecimal memUsed;
    private static BigDecimal dividend;
    private static BigDecimal divisor;
    private static final BigDecimal common = new BigDecimal(100);
    private static final Metric mem = new Metric();

    public static Metric collect() throws IOException {
        String hostname = Files.readString(Paths.get("/etc/hostname"));
        File fileMem = new File("/proc/meminfo");
        InputStream inputStream = new FileInputStream(fileMem);
        //读取
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            //收集相应指标计算cpu和内存使用率
            //MemTotal:        1863028 kB
            String[] split = line.split(":");
            String[] value = split[1].trim().split("\\D+");
            if (split[0].equals(Param.MEMTOTAL) || Param.MEMFREE.equals(split[0]) || Param.BUFFERS.equals(split[0]) || Param.CACHED.equals(split[0])) {
                memMap.put(split[0], Long.parseLong(value[0]));
            }
        }
        reader.close();
        inputStream.close();

        //计算内存使用率
        Long memTotal = memMap.get(Param.MEMTOTAL);
        Long memFree = memMap.get(Param.MEMFREE);
        Long buffers = memMap.get(Param.BUFFERS);
        Long cached = memMap.get(Param.CACHED);
        dividend = BigDecimal.valueOf(memTotal - memFree - buffers - cached);
        divisor = BigDecimal.valueOf(memTotal);
        memUsed = common.multiply(dividend.divide(divisor, 4, RoundingMode.HALF_UP));

        mem.setMetric(Param.MEM);
        mem.setEndpoint(hostname);
        mem.setStep(60L);
        mem.setValue(memUsed.doubleValue());
        mem.setTags(null);

        return mem;

        //memMap.forEach((s,value)->System.out.println(s+":"+value));

    }
}