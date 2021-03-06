package com.xjtu.spider_Assemble.controller;


import com.xjtu.common.domain.RedisName;
import com.xjtu.common.domain.Result;
import com.xjtu.common.domain.ResultEnum;
import com.xjtu.redis.RedisUtil;
import com.xjtu.spider_Assemble.service.SpiderAssembleService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * spider assemble
 * 用于自动构建api的自动碎片爬取
 *
 * @author mkx
 */

@RestController
@RequestMapping("/spiderAssemble")
public class SpiderAssembleController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Jedis jedis;

    static {
        jedis = RedisUtil.getJedis();
    }

    @Autowired
    SpiderAssembleService spiderService;

    @ApiOperation(value = "webmagic自动爬取课程碎片api", notes = "自动构建碎片，webmagic自动爬取课程碎片api")
    @PostMapping("/crawlAssemblesByDomainName")
    public ResponseEntity crawlAssembles(@RequestParam(name = "domainName") String domainName) {
        Result result = spiderService.startCrawlAssembles(domainName);
        if (!result.getCode().equals(ResultEnum.SUCCESS.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @ApiOperation(value = "统计课程碎片", notes = "统计课程碎片")
    @GetMapping("/countAssemblesByDomainName")
    public ResponseEntity countAssembles(@RequestParam(name = "domainName") String domainName) {
        String count = jedis.get(RedisName.ASSEMBLE_NUMBER+"::"+domainName);
        Result result;
        if(count!=null){
            Integer num=Integer.parseInt(count);
            Map<String, Integer> assembleMap = new HashMap<>();
            assembleMap.put("exist", num);
            assembleMap.put("allNumber", 0);
            result=new Result(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(),assembleMap);
        }else{
            result = spiderService.countAssembles(domainName);
            if(result.getData() instanceof Map){
                Map map= (Map) result.getData();
                String num=map.get("exist").toString();
                jedis.set(RedisName.ASSEMBLE_NUMBER+"::"+domainName,num,"NX", "EX", 3600*24*2);
            }
        }
        if (!result.getCode().equals(ResultEnum.SUCCESS.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
