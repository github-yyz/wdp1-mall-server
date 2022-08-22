package com.example.mall.service;

import com.example.mall.mapper.GoodsMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.model.bo.AskGoodsMsgBO;
import com.example.mall.model.po.GoodsPO;
import com.example.mall.model.po.GoodsSpecPO;
import com.example.mall.model.po.MsgPO;
import com.example.mall.model.po.UserPO;
import com.example.mall.model.vo.*;
import com.example.mall.util.MybatisUtils;
import org.apache.ibatis.session.SqlSession;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @Classname MallGoodsServiceImpl
 * @Description
 * @Date 2022-08-22 9:52
 * @Created by Yang Yi-zhou
 */
public class MallGoodsServiceImpl implements MallGoodsService {
    GoodsService goodsService = new GoodsServiceImpl();

    @Override
    public GetGoodsByTypeVO getGoodsByType(Integer typeId) {
        GetGoodsByTypeVO goodsByTypeVO = goodsService.getGoodsByType(typeId);

        return goodsByTypeVO;
    }

    @Override
    public MallSearchGoodsVO searchGoods(String keyword) {
        //mapper
        SqlSession session = MybatisUtils.openSession();
        GoodsMapper mapper = session.getMapper(GoodsMapper.class);
        //根据关键词去搜索
        List<GoodsPO> goodsPOList = mapper.selectGoodsListByKeyword(keyword);
        //VO
        MallSearchGoodsVO mallSearchGoodsVO = new MallSearchGoodsVO();
        //set code
        mallSearchGoodsVO.setCode(0);
        //set data
        List<MallSearchGoodsVO.DataDTO> dataDTOList = new ArrayList<>();
        for (GoodsPO goodsPO : goodsPOList) {
            dataDTOList.add(new MallSearchGoodsVO.DataDTO(goodsPO.getId(), goodsPO.getImg(), goodsPO.getName(), goodsPO.getPrice(), goodsPO.getTypeId()));
        }
        mallSearchGoodsVO.setData(dataDTOList);

        return mallSearchGoodsVO;
    }

    //todo
    @Override
    public GetGoodsMsgVO getGoodsMsg(Integer id) {
        //mapper
        SqlSession session = MybatisUtils.openSession();
        GoodsMapper goodsMapper = session.getMapper(GoodsMapper.class);
        //获取该商品的Msg
        List<MsgPO> msgPOList = goodsMapper.selectMsgListByGoodsId(id);
        //VO
        GetGoodsMsgVO getGoodsMsgVO = new GetGoodsMsgVO();
        //set code
        getGoodsMsgVO.setCode(0);
        //构造data
        List<GetGoodsMsgVO.DataDTO> dataDTOList = new ArrayList<>();
        for (MsgPO msgPO : msgPOList) {
            //根据userid拿到用户名
            UserMapper mapper = session.getMapper(UserMapper.class);
            UserPO userPO = mapper.selectUserById(msgPO.getUserId());

            dataDTOList.add(new GetGoodsMsgVO.DataDTO(msgPO.getId(), msgPO.getContent(), userPO.getNickname(), msgPO.getCreateTime().toString(), new GetGoodsMsgVO.DataDTO.ReplyDTO(msgPO.getReplyContent(), msgPO.getReplyTime().toString())));
        }
        //set data
        getGoodsMsgVO.setData(dataDTOList);


        return getGoodsMsgVO;
    }

    @Override
    public MallGetGoodsInfoVO getGoodsInfo(int id) {
        //mapper
        SqlSession session = MybatisUtils.openSession();
        GoodsMapper goodsMapper = session.getMapper(GoodsMapper.class);
        //VO
        MallGetGoodsInfoVO mallGetGoodsInfoVO = new MallGetGoodsInfoVO();
        //先读商品信息
        GoodsPO goodsPO = goodsMapper.selectGoodsById(id);

        //set code
        mallGetGoodsInfoVO.setCode(0);

        //构造spec
        List<MallGetGoodsInfoVO.DataDTO.SpecsDTO> specsDTOList = new ArrayList<>();
        //根据goodsId去获取规格信息
        List<GoodsSpecPO> goodsSpecPOList = goodsMapper.selectGoodsSpecListByGoodsId(id);
        for (GoodsSpecPO specPO : goodsSpecPOList) {
            specsDTOList.add(new MallGetGoodsInfoVO.DataDTO.SpecsDTO(specPO.getId(), specPO.getName(), specPO.getStockNum(), specPO.getPrice()));
        }

        //构造data
        MallGetGoodsInfoVO.DataDTO dataDTO = new MallGetGoodsInfoVO.DataDTO(goodsPO.getImg(), goodsPO.getName(), goodsPO.getDescription(), goodsPO.getTypeId(), specsDTOList, goodsPO.getPrice());

        //set data
        mallGetGoodsInfoVO.setData(dataDTO);


        return mallGetGoodsInfoVO;
    }

    @Override
    public GetGoodsCommentVO getGoodsComment(int goodsId) {
        return null;
    }

    @Override
    public AskGoodsMsgVO askGoodsMsg(AskGoodsMsgBO askGoodsMsgBO) {
        //解析BO
        String nickname = askGoodsMsgBO.getToken();
        String msg = askGoodsMsgBO.getMsg();
        Integer goodsId = Integer.parseInt(askGoodsMsgBO.getGoodsId());
        //mapper
        SqlSession session = MybatisUtils.openSession();
        GoodsMapper goodsMapper = session.getMapper(GoodsMapper.class);
        //msgPO
        MsgPO msgPO = new MsgPO();
        msgPO.setGoodsId(goodsId);
        msgPO.setContent(msg);
        msgPO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //根据nickname去获取userId
        UserPO userPO = session.getMapper(UserMapper.class).selectUserByNickname(nickname);

        msgPO.setUserId(userPO.getId());
        //msg状态为未回复
        msgPO.setState(1);
        //插入msg表中
        Integer affectedRows = goodsMapper.insertMsgUseMsgPO(msgPO);

        //VO
        AskGoodsMsgVO askGoodsMsgVO = new AskGoodsMsgVO();
        if (affectedRows == 0) {
            askGoodsMsgVO.setCode(10000);
            session.rollback();
        } else {
            askGoodsMsgVO.setCode(0);
            session.commit();
        }
        //关闭资源
        session.close();

        return askGoodsMsgVO;
    }
}