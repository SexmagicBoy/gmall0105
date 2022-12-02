package com.atguigu.gmall.manage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.service.PmsBaseAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class PmsBaseAttrServiceImpl implements PmsBaseAttrService {

    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        // 查询平台属性列表
        Example infoExample = new Example(PmsBaseAttrInfo.class);
        infoExample.createCriteria().andEqualTo("catalog3Id", catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectByExample(infoExample);

        // 为每个属性添加值，封装并返回
        List<PmsBaseAttrInfo> vos = null;
        if (!CollectionUtils.isEmpty(pmsBaseAttrInfos)){
            vos = new ArrayList<>();
            for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
                Example valueExample = new Example(PmsBaseAttrValue.class);
                valueExample.createCriteria().andEqualTo("attrId",pmsBaseAttrInfo.getId());
                List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.selectByExample(valueExample);
                pmsBaseAttrInfo.setAttrValueList(pmsBaseAttrValues);
                vos.add(pmsBaseAttrInfo);
            }
        }
        return vos;
    }

    /**
     * 保存平台属性和属性值列表或更新平台属性列表
     *
     * @param pmsBaseAttrInfo
     * @return
     */
    @Override
    @Transactional
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        if (pmsBaseAttrInfo.getAttrName() == null && pmsBaseAttrInfo.getCatalog3Id() == null) return "参数异常";

        // 如果是更新属性操作会有属性 id，这时将原表中的属性值全部删除，重新保存即可
        if (pmsBaseAttrInfo.getId() != null) {
            // 修改平台属性
            pmsBaseAttrInfoMapper.updateByExample(pmsBaseAttrInfo,
                    new Example(PmsBaseAttrInfo.class)
                            .createCriteria()
                            .andEqualTo("id", pmsBaseAttrInfo.getId()));

            // 修改平台属性值列表
            pmsBaseAttrValueMapper.deleteByExample(new Example(PmsBaseAttrValue.class)
                    .createCriteria()
                    .andEqualTo("attrId", pmsBaseAttrInfo.getId()));

            if (pmsBaseAttrInfo.getAttrValueList() != null) {
                pmsBaseAttrInfo.getAttrValueList().forEach(e -> {
                    pmsBaseAttrValueMapper.insertSelective(e);
                });
            }
        } else {
            // 以下是保存逻辑
            // 保存平台属性
            // insertSelective 和 select 的区别是 insertSelective 只将有值的字段插入，为 null 的则不插入
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            // 保存平台属性值列表
            if (pmsBaseAttrInfo.getAttrValueList() != null) {
                pmsBaseAttrInfo.getAttrValueList().forEach(e -> {
                    e.setAttrId(pmsBaseAttrInfo.getId());
                    pmsBaseAttrValueMapper.insertSelective(e);
                });
            }
        }

        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        Example example = new Example(PmsBaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", attrId);
        return pmsBaseAttrValueMapper.selectByExample(example);
    }
}
