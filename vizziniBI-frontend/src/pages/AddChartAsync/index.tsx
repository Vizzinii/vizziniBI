import {
  UploadOutlined,

} from '@ant-design/icons';
import {
  Button, Card, Form, Input, message,
  Select,
  Space,
  Upload,
} from 'antd';
import React, {useState} from 'react';
import TextArea from "antd/es/input/TextArea";
import {chartAutoAnalysisUsingPost} from "@/services/vizziniBI/chartController";
import {useForm} from "antd/es/form/Form";

/**
 * 添加图表页面（异步）
 * @constructor
 */
const AddChartAsync: React.FC = () => {

  const [form] = useForm();
  const [submitting, setSubmitting] = useState<boolean>(false);

  const onFinish = async (values: any) => {
    if (submitting) {
      return;
    }
    setSubmitting(true);
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
        message.success('分析任务提交成功，一分钟内在历史图表页面内查收');
        form.resetFields();
      }
    } catch (e: any) {
      message.error('分析失败，' + e.message);
    }
    setSubmitting(false);
  };

  return (
    <div className="add-chart-async">
      <Card title="图表智能分析">
        <Form form={form}
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
                提交生成请求
              </Button>
              <Button htmlType="reset">重置预设</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
export default AddChartAsync;
