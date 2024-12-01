import { listChartByPageUsingPost } from '@/services/vizziniBI/chartController';
import {Avatar, Card, Divider, List, message} from 'antd';
import React, {useEffect, useState} from 'react';
import ReactECharts from "echarts-for-react";

import {getInitialState} from "@/app";
import {useModel} from "@@/plugin-model";
import {column} from "stylis";
import Search from "antd/es/input/Search";

/**
 * 我的历史图表
 * @constructor
 */
const MyChartPage: React.FC = () => {
  const initSearchParams = {
    current: 1, // 初始是第一页
    pageSize: 4, // 每一页展示4条
    sortField: 'createTime',
    sortOrder: 'asc',
  };

  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ ...initSearchParams });
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const [ loading, setLoading ] = useState<boolean>(true);
  const { initialState ,setInitialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listChartByPageUsingPost(searchParams);
      if (res.data) {
        // if (res.data.records) {
        //   res.data.records.forEach((data) => {
        //     if (data.status === 'success') {
        //       const chartOption = JSON.parse(data.genChart ?? '{}');
        //       chartOption.title = undefined;
        //       data.genChart = JSON.stringify(chartOption);
        //     }
        //   });
        // }
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0);
        // 隐藏图表的title
        if (res.data.records) {
          res.data.records.forEach(data => {
            if (data.isDelete === 0) { // 如果这个表项没有被删除
              const chartOption = JSON.parse(data.genChart ?? '{}');
              chartOption.title = undefined;
              data.genChart = JSON.stringify(chartOption);
            }
          })
        }
      } else {
        message.error('获取我的图表失败');
      }
    } catch (e: any) {
      message.error('获取历史图表失败，' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  return (
    <div className="my-chart">
      哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇哇数据列表：
      <Divider />
      {/*{JSON.stringify(chartList)}*/}
      <div>
        <Search placeholder={'在找哪个图表呢'} enterButton onSearch={(value) => {
          setSearchParams({
            ...initSearchParams,
            name: value,
          })
        }}/>
      </div>
      <br/>
      <List
        grid={{
          gutter: 16, //两列之间的间距
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
        }}
        size="large"
        pagination={{
          onChange: (page) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize: searchParams.pageSize,
            })
            console.log(page);
          },
          current: searchParams.current, // 当前是第几页
          pageSize: searchParams.pageSize, // 一页展示多少条
          total: total,
        }}
        loading={loading}
        dataSource={chartList}
        footer={
          <div>
            <b>ant design</b> footer part
          </div>
        }
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card>
              <List.Item.Meta
                avatar={<Avatar src={currentUser && currentUser.userAvatar}/>}
                title={item.name}
                description={item.goal + ' （' + item.chartType + '）'}
              />
              <div style={{marginBottom: 16}}/>
              {item.genResult}
              <div style={{marginBottom: 16}}/>
              <ReactECharts option={item.genChart && JSON.parse(item.genChart)}/>
            </Card>
          </List.Item>
        )}
      />
      历史图表总数：{total}
    </div>
  );
};
export default MyChartPage;
