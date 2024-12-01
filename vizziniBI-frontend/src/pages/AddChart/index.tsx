import {
  UploadOutlined,

} from '@ant-design/icons';
import {
  Button, Card, Col, Divider,
  Form, Input, message, Row,
  Select,
  Space, Spin,
  Upload,
} from 'antd';
import {createStyles} from 'antd-style';
import React, {useState} from 'react';
import ReactECharts from 'echarts-for-react';
import TextArea from "antd/es/input/TextArea";
import {chartAutoAnalysisUsingPost} from "@/services/vizziniBI/chartController";

const useStyles = createStyles(({token}) => {
  return {
    action: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.2)',
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});

/**
 * 返回显示的结果
 */


/**
 * 添加图表页面
 * @constructor
 */
const Addchart: React.FC = () => {

  const [chart, setChart] = useState<API.BIResponse>();
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [option, setOption] = useState<any>();

  const onFinish = async (values: any) => {
    if (submitting) {
      return;
    }
    setSubmitting(true);
    setOption(undefined);
    setChart(undefined);
    const params = {
      ...values,
      file: undefined
    }
    try {
      const res = await chartAutoAnalysisUsingPost(params, {}, values.file.file.originFileObj)
      console.log('res', res);
      if (!res?.data) {
        message.error('分析失败,系统发生错误');
      } else {
        //message.success('分析成功');
        const chartOption = JSON.parse(res?.data.genChart ?? '');
        if (!chartOption) {
          throw new Error('图表解析错误')
        } else {
          message.success('分析成功');
          setChart(res.data);
          setOption(chartOption);
        }

      }
    } catch (e: any) {
      message.error('分析失败，' + e.message);
    }
    setSubmitting(false);
  };

  return (
    <div className="add-chart">
      <Row gutter={24}>
        <Col span={12}>
          <Card title="图表智能分析">
            <Form
              name="add-chart"
              onFinish={onFinish}
              initialValues={{}}
            >

              <Form.Item name="name" label="图表名字" rules={[{required: true}]}>
                <Input placeholder="给即将生成的图表起个名字吧"/>
              </Form.Item>


              <Form.Item name="goal" label="分析目标" rules={[{required: true}]}>
                <TextArea placeholder="请输入你的分析要求，如“分析表格内用户增长趋势”"/>
              </Form.Item>


              <Form.Item
                name="file"
                label="原始数据表格"
                extra="请选择要上传的文件（仅支持.xlsx类型文件）"
                rules={[{required: true}]}
              >
                <Upload name="file">
                  <Button icon={<UploadOutlined/>}>点击上传.xlsx文件</Button>
                </Upload>
              </Form.Item>


              <Form.Item
                name="chartType"
                label="图表类型"
                hasFeedback
                style={{width: 120}}
                rules={[{required: false, message: '请选择要生成的图表类型'}]}
              >
                <Select
                  defaultValue="折线图"
                  style={{width: 120}}
                  options={[
                    {value: '折线图', label: '折线图'},
                    {value: '柱状图', label: '柱状图'},
                    {value: '饼状图', label: '饼状图'},
                    {value: '面积图', label: '面积图'},
                    {value: '雷达图', label: '雷达图'},
                  ]}
                />
              </Form.Item>


              <Form.Item wrapperCol={{span: 12, offset: 6}}>
                <Space>
                  <Button type="primary" htmlType="submit" loading={submitting} disabled={submitting}>
                    开始生成
                  </Button>
                  <Button htmlType="reset">重置预设</Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </Col>
        <Col span={12}>
            <Card title="分析结论">
              {
                chart?.genResult ?? <div>请先在左侧填写好表单再点击生成！</div>
              }
              <Spin spinning={submitting}></Spin>
            </Card>
            <Divider></Divider>
            <Card title="可视化结果">
              {
                option ? <ReactECharts option={option}/> : <div>请先在左侧填写好表单再点击生成！</div>
              }
              <Spin spinning={submitting}></Spin>
            </Card>
        </Col>
      </Row>
    </div>
  );
};
export default Addchart;
