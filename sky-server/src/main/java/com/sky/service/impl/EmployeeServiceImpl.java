package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的明文密码进行md5加密处理，存入数据库
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /*
    * 新增员工
    *
    * @param employeeDTO
    * @return
    */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        //将employee里面和employeeDTO的同名参数传入employeeDTO，其余不同名的参数需要手动设置
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码
        //md5DigestAsHex只接收字节数组
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //10L Long类型字符
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //TODO 这里可能已经存在username,但是还是插入，插入的时候数据库报重名错误才会捕获，此时id已经自增，不会回滚，暂时没做防重查询
        //所以这里应该先查询数据库中是否存在该username，如果存在则抛出异常，如果不存在则插入
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     **/
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
//      pageHelper插件只是传当前页码和数据量，其余的查询条件并没有作为参数传过去，把page和size通过localThread存储，所以下面一行代码会接收到page和size
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page =  employeeMapper.pageQuery(employeePageQueryDTO);
        
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total , records);
    }

    /**
     * 切换员工账号状态，禁用/启用
     * @param id
     * @param status
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        //要动态写入.不是直接把id和status传入，而是通过Employee对象来传入,update方法要能复用
        Employee employee = Employee.builder()
                        .id(id)
                        .status(status)
                        .build();
        employeeMapper.update(employee);

    }

}
